export class VersionConflictError<T = unknown> extends Error {
    public latest: T

    constructor(latest: T, name: string = "VersionConflictError") {
        super("VERSION_CONFLICT")
        this.name = name
        this.latest = latest
        Object.setPrototypeOf(this, VersionConflictError.prototype)
    }
}